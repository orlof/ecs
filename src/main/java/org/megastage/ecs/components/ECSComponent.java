package org.megastage.ecs.components;

import org.jdom2.Element;
import org.megastage.ecs.messages.ECSMessage;

public interface ECSComponent {
    int cid = -1;
    default int cid() { return -1; }

    /** This method is called when initial state component is created from template **/
    default void config(int eid, Element elem) {}

    /** This method is called after world is ready **/
    default void initialize() {}

    /** This method is called when entity is deleted **/
    default void delete(int eid) {}

    default ECSMessage transmit() { return null; }
}
