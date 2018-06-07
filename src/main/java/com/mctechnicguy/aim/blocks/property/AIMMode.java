package com.mctechnicguy.aim.blocks.property;

public class AIMMode implements IMode, Comparable<AIMMode> {

    private String name;
    private int id;

    AIMMode(String modeName, int id) {
        this.name = modeName;
        this.id = id;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IMode) {
            return ((IMode)obj).getName().equals(this.getName()) && ((IMode)obj).getID() == this.getID();
        }
        else return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + 10*this.getID();
    }

    @Override
    public int compareTo(AIMMode o) {
        return Integer.compare(this.getID(), o.getID());
    }
}
