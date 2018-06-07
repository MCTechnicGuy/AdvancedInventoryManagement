package com.mctechnicguy.aim.blocks.property;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.block.properties.PropertyHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class PropertyAIMMode extends PropertyHelper<AIMMode> {

    private final ImmutableSet<AIMMode> allowedValues;
    /** Map of names to Enum values */
    private final Map<String, AIMMode> nameToValue = Maps.newHashMap();
    private final ArrayList<AIMMode> sortedValues = new ArrayList<>();

    protected PropertyAIMMode(@Nonnull String name, @Nonnull String... modeNames)
    {
        super(name, AIMMode.class);

        if (modeNames.length <= 0) {
            throw new IllegalArgumentException("PropertyAIMMode has to have at least one mode configured!");
        }

        int id = 0;
        for (String modeName : modeNames) {
            AIMMode newValue = new AIMMode(modeName, id);
            this.nameToValue.put(modeName, newValue);
            sortedValues.add(newValue);
            id++;
        }

        this.allowedValues = ImmutableSet.copyOf(nameToValue.values());

    }

    public static PropertyAIMMode create(@Nonnull String name, @Nonnull String... modeNames) {
        return new PropertyAIMMode(name, modeNames);
    }

    @Nonnull
    public AIMMode getModeForID(int id) {
        if (id <= 0 || id > sortedValues.size() - 1) return sortedValues.get(0);
        return sortedValues.get(id);
    }

    @Override
    public Collection<AIMMode> getAllowedValues() {
        return allowedValues;
    }

    @Override
    public Optional<AIMMode> parseValue(String value) {
        return Optional.fromNullable(this.nameToValue.get(value));
    }

    @Override
    public String getName(AIMMode value) {
        return value.getName();
    }
}
