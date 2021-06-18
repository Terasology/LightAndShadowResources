// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.lightandshadowresources.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.world.block.family.BlockFamily;

/**
 * This is the component class for playing cards, which are constructed of a top and a bottom block.
 */
public class CardComponent implements Component {
    public BlockFamily topBlockFamily;
    public BlockFamily bottomBlockFamily;
    public Prefab cardBlockPrefab;
}
