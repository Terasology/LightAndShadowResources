// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadowresources.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.items.AddToBlockBasedItem;

@AddToBlockBasedItem
public class FlagComponent implements Component {
    public String team;
}
