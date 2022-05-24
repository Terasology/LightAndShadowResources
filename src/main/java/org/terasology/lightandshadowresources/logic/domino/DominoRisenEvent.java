/*
 * Copyright 2022 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.   //this file is closed door event
 */
package org.terasology.lightandshadowresources.logic.domino;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

@ServerEvent
public class DominoRisenEvent implements Event {
    private EntityRef dominoEntity;

    public DominoRisenEvent() {
        dominoEntity = EntityRef.NULL;
    }

    public DominoRisenEvent(EntityRef dominoEntity) {
        this.dominoEntity = dominoEntity;
    }

    public EntityRef getDominoEntity() {
        return dominoEntity;
    }
}