package com.example.myproject.components.pokemon.viewModel;

import java.util.ArrayList;
import java.util.List;

public final class PokemonViewModelList {

    public PokemonViewModelList() {
        pokemons = new ArrayList<>();
    }

    public PokemonViewModelList(List<PokemonViewModel> pokemons) {
        this.pokemons = pokemons;
    }

    public List<PokemonViewModel> pokemons;
}