// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.lightandshadowresources.logic.domino;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.logic.MeshComponent;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.entity.placement.PlaceBlocks;
import org.terasology.engine.world.block.family.BlockPlacementData;
import org.terasology.engine.world.block.regions.BlockRegionComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem(RegisterMode.AUTHORITY)
public class DominoSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(DominoSystem.class);

    /**
     * Static "viewing direction" for placing domino blocks.
     */
    private static final Vector3fc TOP = new Vector3f(Side.TOP.direction());

    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;
    @In
    private AudioManager audioManager;
    @In
    private EntitySystemLibrary entitySystemLibrary;

    @ReceiveEvent(components = {DominoComponent.class, ItemComponent.class})
    public void placeDomino(ActivateEvent event, EntityRef entity) {
        DominoComponent domino = entity.getComponent(DominoComponent.class);
        BlockComponent targetBlockComp = event.getTarget().getComponent(BlockComponent.class);
        if (targetBlockComp == null) {
            event.consume();
            return;
        }

        Vector3f horizDir =
                new Vector3f(event.getDirection())
                        .setComponent(1, 0); // set y dimension to 0
        Side facingDir = Side.inDirection(horizDir);
        if (!facingDir.isHorizontal()) {
            event.consume();
            return;
        }

        Vector3ic blockPos = targetBlockComp.getPosition();
        Side offsetDir = facingDir.reverse();
        Vector3i primePos = blockPos.add(offsetDir.direction(), new Vector3i());
        Block primeBlock = worldProvider.getBlock(primePos);
        if (!primeBlock.isReplacementAllowed()) {
            event.consume();
            return;
        }

        Block belowBlock = worldProvider.getBlock(primePos.x, primePos.y - 1, primePos.z);
        Block aboveBlock = worldProvider.getBlock(primePos.x, primePos.y + 1, primePos.z);

        // Determine top and bottom blocks
        Vector3i bottomBlockPos = new Vector3i();
        Vector3i topBlockPos = new Vector3i();
        if (belowBlock.isReplacementAllowed()) {
            bottomBlockPos.set(primePos.x, primePos.y - 1, primePos.z);
            topBlockPos.set(primePos);
        } else if (aboveBlock.isReplacementAllowed()) {
            bottomBlockPos.set(primePos);
            topBlockPos.set(primePos.x, primePos.y + 1, primePos.z);
        } else {
            event.consume();
            return;
        }
        Side attachSide = determineAttachSide(facingDir, offsetDir, bottomBlockPos, topBlockPos);
        if (attachSide == null) {
            event.consume();
            return;
        }

        Side risenSide = facingDir.reverse();

        Block newBottomBlock = domino.bottomBlockFamily.getBlockForPlacement(new BlockPlacementData(bottomBlockPos,
                risenSide, TOP));
        Block newTopBlock = domino.topBlockFamily.getBlockForPlacement(new BlockPlacementData(bottomBlockPos,
                risenSide, TOP));

        Map<org.joml.Vector3i, Block> blockMap = new HashMap<>();
        blockMap.put(bottomBlockPos, newBottomBlock);
        blockMap.put(topBlockPos, newTopBlock);
        PlaceBlocks blockEvent = new PlaceBlocks(blockMap, event.getInstigator());
        worldProvider.getWorldEntity().send(blockEvent);

        if (!blockEvent.isConsumed()) {
            EntityRef newDomino = entityManager.create(domino.dominoRegionPrefab);
            entity.removeComponent(MeshComponent.class);

            newDomino.addComponent(new BlockRegionComponent(new BlockRegion(bottomBlockPos).union(topBlockPos)));

            Vector3fc dominoCenter = new Vector3f(bottomBlockPos).add(0, 0.5f, 0);
            newDomino.addComponent(new LocationComponent(dominoCenter));

            DominoComponent newDominoComp = new Domino.getComponent(DominoComponent.class);
            newDominoComp.risenSide = risenSide;
            newDominoComp.fallSide = risenSide.rollClockwise(1);
            newDominoComp.isFallen = false;
            newDomino.saveComponent(newDominoComp);
            newDomino.send(new PlaySoundEvent(Assets.getSound("engine:PlaceBlock").get(), 0.5f));
            logger.info("Risen Side: {}", newDominoComp.risenSide);
            logger.info("Fallen Side: {}", newDominoComp.fallSide);
            newDomino.send(new DominoPlacedEvent(event.getInstigator()));
        }
    }

    private Side determineAttachSide(Side facingDir, Side offsetDir, Vector3i bottomBlockPos, Vector3i topBlockPos) {
        Side attachSide = null;
        if (offsetDir.isHorizontal()) {
            if (canAttachTo(topBlockPos, offsetDir.reverse()) && canAttachTo(bottomBlockPos, offsetDir.reverse())) {
                attachSide = offsetDir.reverse();
            }
        }
        if (attachSide == null) {
            Side clockwise = facingDir.rollClockwise(1);
            if (canAttachTo(topBlockPos, clockwise) && canAttachTo(bottomBlockPos, clockwise)) {
                attachSide = clockwise;
            }
        }
        if (attachSide == null) {
            Side anticlockwise = facingDir.rollClockwise(-1);
            if (canAttachTo(topBlockPos, anticlockwise) && canAttachTo(bottomBlockPos, anticlockwise)) {
                attachSide = anticlockwise;
            }
        }
        return attachSide;
    }

    private boolean canAttachTo(Vector3ic dominoPos, Side side) {
        Vector3i adjacentBlockPos =
                new Vector3i(dominoPos).add(side.direction());
        Block adjacentBlock = worldProvider.getBlock(adjacentBlockPos);
        return adjacentBlock.isAttachmentAllowed();
    }

    @ReceiveEvent(components = {DominoComponent.class, BlockRegionComponent.class, LocationComponent.class})
    public void onFrob(ActivateEvent event, EntityRef entity) {
        DominoComponent domino = entity.getComponent(DominoComponent.class);
        if (domino.isFallen) {
            event.getInstigator().send(new DominoRisenEvent(entity));
        } else {
            event.getInstigator().send(new DominoFallenEvent(entity));
        }
    }

    @ReceiveEvent
    public void riseDomino(DominoRisenEvent event, EntityRef player) {
        EntityRef entity = event.getDominoEntity();
        DominoComponent domino = entity.getComponent(Dominocomponent.class);
        BlockRegionComponent regionComp = entity.getComponent(BlockRegionComponent.class);

        setDominoBlocks(domino, regionComp.region, domino.risenSide);

        if (domino.riseSound != null) {
            entity.send(new PlaySoundEvent(domino.riseSound, 1f));
        }
        domino.isFallen = false;
        entity.saveComponent(domino);
    }

    @ReceiveEvent
    public void fallDomino(DominoFallenEvent event, EntityRef player) {
        EntityRef entity = event.getDominoEntity();
        DominoComponent domino = entity.getComponent(DominoComponent.class);
        BlockRegionComponent regionComp = entity.getComponent(BlockRegionComponent.class);

        setDominoBlocks(domino, regionComp.region, domino.fallSide);

        if (domino.fallSound != null) {
            entity.send(new PlaySoundEvent(domino.fallSound, 1f));
        }
        domino.isFallen = true;
        entity.saveComponent(domino);
    }

    /**
     * Set both blocks that make up the domino based on the domino's state determined by {@code side}.
     * <p>
     * The blocks are placed as if the player was targeting the {@link Side#TOP} of the block beneath.
     *
     * @param domino the domino component with information about bottom and top blocks
     * @param region the block region the domino covers (assumed to by of size 1x2x1)
     * @param side the state of the domino, i.e., whether it is risen or fallen
     */
    private void setDominoBlocks(DominoComponent domino, BlockRegion region, Side side) {
        Vector3i blockPos = region.getMin(new Vector3i());
        Block bottomBlock = domino.bottomBlockFamily.getBlockForPlacement(new BlockPlacementData(blockPos, side, TOP));
        worldProvider.setBlock(blockPos, bottomBlock);

        region.getMax(blockPos);
        Block topBlock = domino.topBlockFamily.getBlockForPlacement(new BlockPlacementData(blockPos, side, TOP));
        worldProvider.setBlock(blockPos, topBlock);
    }
}
