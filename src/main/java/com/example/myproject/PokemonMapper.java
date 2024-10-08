package com.example.myproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;

/**
 * Maps database statements to {@link com.example.myproject.Pokemon} class.
 */
public class PokemonMapper implements DbMapper<Pokemon> {

    @Override
    public Pokemon read(DbRow row) {
        DbColumn id = row.column("id");
        DbColumn name = row.column("name");
        DbColumn type = row.column("idType");
        return new Pokemon(id.get(Integer.class), name.get(String.class), type.get(Integer.class));
    }

    @Override
    public Map<String, Object> toNamedParameters(Pokemon value) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("id", value.id());
        map.put("name", value.name());
        map.put("idType", value.idType());
        return map;
    }

    @Override
    public List<Object> toIndexedParameters(Pokemon value) {
        List<Object> list = new ArrayList<>(3);
        list.add(value.id());
        list.add(value.name());
        list.add(value.idType());
        return list;
    }
}
