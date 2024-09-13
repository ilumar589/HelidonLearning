package com.example.myproject.components.pokemon.viewModel;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public record PokemonViewModelList(ImmutableList<PokemonViewModel> pokemons) {
    public PokemonViewModelList {
        if (pokemons == null) {
            pokemons = Lists.immutable.empty();
        }
    }
}