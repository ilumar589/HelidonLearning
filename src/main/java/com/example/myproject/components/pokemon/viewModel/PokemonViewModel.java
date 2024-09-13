package com.example.myproject.components.pokemon.viewModel;

public record PokemonViewModel(int id, String name) {

    public PokemonViewModel {
        if (name == null) {
            name = "";
        }
    }
}
