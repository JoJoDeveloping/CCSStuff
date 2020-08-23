package de.jojomodding.np.lts;

import java.util.Objects;

public final class Channel {

    private final String name;

    public Channel(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Channel channel = (Channel) o;
        return name.equals(channel.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
