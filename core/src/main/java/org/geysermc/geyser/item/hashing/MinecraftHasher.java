/*
 * Copyright (c) 2025 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.item.hashing;

import com.google.common.base.Suppliers;
import com.google.common.hash.HashCode;
import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtList;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.geyser.inventory.item.DyeColor;
import org.geysermc.geyser.item.components.Rarity;
import org.geysermc.geyser.item.hashing.data.FireworkExplosionShape;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EquipmentSlot;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.GlobalPos;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.Consumable;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.Filterable;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.ItemAttributeModifiers;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.Unit;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("UnstableApiUsage")
@FunctionalInterface
public interface MinecraftHasher<T> {

    MinecraftHasher<Unit> UNIT = (unit, encoder) -> encoder.emptyMap();

    MinecraftHasher<Byte> BYTE = (b, encoder) -> encoder.number(b);

    MinecraftHasher<Short> SHORT = (s, encoder) -> encoder.number(s);

    MinecraftHasher<Integer> INT = (i, encoder) -> encoder.number(i);

    MinecraftHasher<Long> LONG = (l, encoder) -> encoder.number(l);

    MinecraftHasher<Float> FLOAT = (f, encoder) -> encoder.number(f);

    MinecraftHasher<Double> DOUBLE = (d, encoder) -> encoder.number(d);

    MinecraftHasher<String> STRING = (s, encoder) -> encoder.string(s);

    MinecraftHasher<Boolean> BOOL = (b, encoder) -> encoder.bool(b);

    MinecraftHasher<IntStream> INT_ARRAY = (ints, encoder) -> encoder.intArray(ints.toArray());

    MinecraftHasher<NbtMap> NBT_MAP = (map, encoder) -> encoder.nbtMap(map);

    MinecraftHasher<NbtList<?>> NBT_LIST = (list, encoder) -> encoder.nbtList(list);

    MinecraftHasher<Vector3i> POS = INT_ARRAY.convert(pos -> IntStream.of(pos.getX(), pos.getY(), pos.getZ()));

    MinecraftHasher<Key> KEY = STRING.convert(Key::asString);

    MinecraftHasher<Key> TAG = STRING.convert(key -> "#" + key.asString());

    MinecraftHasher<UUID> UUID = INT_ARRAY.convert(uuid -> {
        long mostSignificant = uuid.getMostSignificantBits();
        long leastSignificant = uuid.getLeastSignificantBits();
        return IntStream.of((int) (mostSignificant >> 32), (int) mostSignificant, (int) (leastSignificant >> 32), (int) leastSignificant);
    }); // TODO test

    MinecraftHasher<GameProfile.Property> GAME_PROFILE_PROPERTY = mapBuilder(builder -> builder
        .accept("name", STRING, GameProfile.Property::getName)
        .accept("value", STRING, GameProfile.Property::getValue)
        .optionalNullable("signature", STRING, GameProfile.Property::getSignature));

    MinecraftHasher<GameProfile> GAME_PROFILE = mapBuilder(builder -> builder
        .optionalNullable("name", STRING, GameProfile::getName)
        .optionalNullable("id", UUID, GameProfile::getId)
        .optionalList("properties", GAME_PROFILE_PROPERTY, GameProfile::getProperties));

    MinecraftHasher<Integer> RARITY = fromIdEnum(Rarity.values(), Rarity::getName);

    MinecraftHasher<Integer> DYE_COLOR = fromIdEnum(DyeColor.values(), DyeColor::getJavaIdentifier);

    MinecraftHasher<Consumable.ItemUseAnimation> ITEM_USE_ANIMATION = fromEnum();

    MinecraftHasher<EquipmentSlot> EQUIPMENT_SLOT = fromEnum(slot -> switch (slot) {
        case MAIN_HAND -> "mainhand";
        case OFF_HAND -> "offhand";
        case BOOTS -> "feet";
        case LEGGINGS -> "legs";
        case CHESTPLATE -> "chest";
        case HELMET -> "head";
        case BODY -> "body";
        case SADDLE -> "saddle";
    });

    MinecraftHasher<ItemAttributeModifiers.EquipmentSlotGroup> EQUIPMENT_SLOT_GROUP = fromEnum(group -> switch (group) {
        case ANY -> "any";
        case MAIN_HAND -> "mainhand";
        case OFF_HAND -> "offhand";
        case HAND -> "hand";
        case FEET -> "feet";
        case LEGS -> "legs";
        case CHEST -> "chest";
        case HEAD -> "head";
        case ARMOR -> "armor";
        case BODY -> "body";
        case SADDLE -> "saddle";
    });

    MinecraftHasher<GlobalPos> GLOBAL_POS = mapBuilder(builder -> builder
        .accept("dimension", KEY, GlobalPos::getDimension)
        .accept("pos", POS, GlobalPos::getPosition));

    MinecraftHasher<Integer> FIREWORK_EXPLOSION_SHAPE = fromIdEnum(FireworkExplosionShape.values());

    HashCode hash(T value, MinecraftHashEncoder encoder);

    default MinecraftHasher<List<T>> list() {
        return (list, encoder) -> encoder.list(list.stream().map(element -> hash(element, encoder)).toList());
    }

    default MinecraftHasher<Filterable<T>> filterable() {
        return mapBuilder(builder -> builder
            .accept("raw", this, Filterable::getRaw)
            .optionalNullable("filtered", this, Filterable::getOptional));
    }

    default <D> MinecraftHasher<D> dispatch(Function<D, T> typeExtractor, Function<T, MapBuilder<D>> hashDispatch) {
        return dispatch("type", typeExtractor, hashDispatch);
    }

    default <D> MinecraftHasher<D> dispatch(String typeKey, Function<D, T> typeExtractor, Function<T, MapBuilder<D>> hashDispatch) {
        return mapBuilder(builder -> builder
            .accept(typeKey, this, typeExtractor)
            .accept(hashDispatch, typeExtractor));
    }

    default <C> MinecraftHasher<C> convert(Function<C, T> converter) {
        return (value, encoder) -> hash(converter.apply(value), encoder);
    }

    default <C> MinecraftHasher<C> sessionConvert(BiFunction<GeyserSession, C, T> converter) {
        return (value, encoder) -> hash(converter.apply(encoder.session(), value), encoder);
    }

    static <T> MinecraftHasher<T> lazyInitialize(Supplier<MinecraftHasher<T>> hasher) {
        Supplier<MinecraftHasher<T>> memoized = Suppliers.memoize(hasher::get);
        return (value, encoder) -> memoized.get().hash(value, encoder);
    }

    static <T> MinecraftHasher<T> recursive(UnaryOperator<MinecraftHasher<T>> delegate) {
        return new Recursive<>(delegate);
    }

    static <T extends Enum<T>> MinecraftHasher<Integer> fromIdEnum(T[] values) {
        return fromIdEnum(values, t -> t.name().toLowerCase());
    }

    static <T extends Enum<T>> MinecraftHasher<Integer> fromIdEnum(T[] values, Function<T, String> toName) {
        return STRING.convert(id -> toName.apply(values[id]));
    }

    // TODO: note that this only works correctly if enum constants are named appropriately
    static <T extends Enum<T>> MinecraftHasher<T> fromEnum() {
        return fromEnum(t -> t.name().toLowerCase());
    }

    static <T extends Enum<T>> MinecraftHasher<T> fromEnum(Function<T, String> toName) {
        return STRING.convert(toName);
    }

    static <T> MinecraftHasher<T> mapBuilder(MapBuilder<T> builder) {
        return (value, encoder) -> builder.apply(new MapHasher<>(value, encoder)).build();
    }

    static <K, V> MinecraftHasher<Map<K, V>> map(MinecraftHasher<K> keyHasher, MinecraftHasher<V> valueHasher) {
        return (map, encoder) -> encoder.map(map.entrySet().stream()
            .map(entry -> Map.entry(keyHasher.hash(entry.getKey(), encoder), valueHasher.hash(entry.getValue(), encoder)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    static <T, F, S> MinecraftHasher<T> either(MinecraftHasher<F> firstHasher, Function<T, F> firstExtractor, MinecraftHasher<S> secondHasher, Function<T, S> secondExtractor) {
        return (value, encoder) -> {
            F first = firstExtractor.apply(value);
            if (first != null) {
                return firstHasher.hash(first, encoder);
            }
            return secondHasher.hash(secondExtractor.apply(value), encoder);
        };
    }

    static <T> MinecraftHasher<T> dispatch(Function<T, MinecraftHasher<T>> hashDispatch) {
        return (value, encoder) -> hashDispatch.apply(value).hash(value, encoder);
    }

    class Recursive<T> implements MinecraftHasher<T> {
        private final Supplier<MinecraftHasher<T>> delegate;

        public Recursive(UnaryOperator<MinecraftHasher<T>> delegate) {
            this.delegate = Suppliers.memoize(() -> delegate.apply(this));
        }

        @Override
        public HashCode hash(T value, MinecraftHashEncoder encoder) {
            return delegate.get().hash(value, encoder);
        }
    }
}
