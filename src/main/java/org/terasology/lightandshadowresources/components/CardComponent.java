// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadowresources.components;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * This is the component class for playing cards, which are constructed of a top and a bottom block.
 */
public class CardComponent implements Component<CardComponent> {
    public BlockFamily topBlockFamily;
    public BlockFamily bottomBlockFamily;
    public Prefab cardBlockPrefab;

    @Override
    public void copy(CardComponent other) {
        this.topBlockFamily = other.topBlockFamily;
        this.bottomBlockFamily = other.bottomBlockFamily;
        this.cardBlockPrefab = other.cardBlockPrefab;
    }
}
