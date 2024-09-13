package com.example.myproject.components.pokemon;

import com.example.myproject.components.pokemon.viewModel.PokemonViewModel;
import com.example.myproject.components.pokemon.viewModel.PokemonViewModelList;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import org.eclipse.collections.api.factory.Lists;

public final class PokemonListService implements HttpService {

    private final PokemonViewModelList localDb;

    public PokemonListService() {
        localDb = new PokemonViewModelList(Lists.immutable.of(
                new PokemonViewModel(1, "Pikachu"),
                new PokemonViewModel(2, "Charizard"),
                new PokemonViewModel(3, "Bulbasaur")));
    }

    @Override
    public void routing(HttpRules httpRules) {
//        httpRules.register("/", StaticContentService.builder("views/pokemonList")
//                .welcomeFileName("pokemonList.rocker.html")
//                .build());

        httpRules
                .get("/", this::index)
                .get("/pokemonList", this::getPokemonList)
                .get("/pokemon/{id}/edit", this::editPokemon);
    }

    private void index(ServerRequest request, ServerResponse response) {
        System.out.println("index");

        response.headers().contentType(MediaTypes.TEXT_HTML);

        final var out = (ArrayOfByteArraysOutput) pokemonList.pokemonIndex
                .template()
                .render();

        response.send(out.toByteArray());

//        try (final var view = FileResourceUtils.getResourceAsStream("views/pokemonList/pokemonIndex.rocker.html")) {
//            response.send(view.readAllBytes());
//        } catch (IOException e) {
//            //
//        }
    }

    private void getPokemonList(ServerRequest request, ServerResponse response) {
        System.out.println("getPokemonList");

        response.headers().contentType(MediaTypes.TEXT_HTML);

        final var out = (ArrayOfByteArraysOutput) pokemonList.pokemonList
                .template(localDb)
                .render();

        response.send(out.toByteArray());

//        try (final var view = FileResourceUtils.getResourceAsStream("views/pokemonList/pokemonList.rocker.html")) {
//            response.send(view.readAllBytes());
//        } catch (IOException e) {
//            //
//        }
    }

    private void editPokemon(ServerRequest request, ServerResponse response) {
        int pokemonId = Integer.parseInt(request.path()
                .pathParameters()
                .get("id"));

        final var searchResult = localDb.pokemons().get(pokemonId - 1);

        final var out = (ArrayOfByteArraysOutput) pokemonList.pokemonEdit
                .template(searchResult)
                .render();

        response.headers().contentType(MediaTypes.TEXT_HTML);
        response.send(out.toByteArray());
    }


}
