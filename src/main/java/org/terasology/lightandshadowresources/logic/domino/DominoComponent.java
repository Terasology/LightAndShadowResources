// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.lightandshadowresources.logic.domino;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 */
public class DominoComponent implements Component<DominoComponent> {
    public BlockFamily topBlockFamily;
    public BlockFamily bottomBlockFamily;
    public Side closedSide;
    public Side openSide;
    public StaticSound openSound;
    public StaticSound closeSound;
    public Prefab dominoRegionPrefab;

    public boolean isOpen;

    @Override
    public void copyFrom(DominoComponent other) {
        this.topBlockFamily = other.topBlockFamily;
        this.bottomBlockFamily = other.bottomBlockFamily;
        this.closedSide = other.closedSide;
        this.openSide = other.openSide;
        this.openSound = other.openSound;
        this.closeSound = other.closeSound;
        this.dominoRegionPrefab = other.dominoRegionPrefab;
        this.isOpen = other.isOpen;
    }
}