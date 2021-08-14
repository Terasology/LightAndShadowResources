// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadowresources.components;

import org.terasology.engine.world.block.items.AddToBlockBasedItem;
import org.terasology.gestalt.entitysystem.component.Component;

@AddToBlockBasedItem
public class FlagComponent implements Component<FlagComponent> {
    public String team;

    @Override
    public void copyFrom(FlagComponent other) {
        this.team = other.team;
    }
}
