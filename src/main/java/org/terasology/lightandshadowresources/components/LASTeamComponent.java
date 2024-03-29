// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadowresources.components;

import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

public final class LASTeamComponent implements Component<LASTeamComponent> {
    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public String team;

    public LASTeamComponent() {
    }

    public LASTeamComponent(String team) {
        this.team = team;
    }

    @Override
    public void copyFrom(LASTeamComponent other) {
        this.team = other.team;
    }
}
