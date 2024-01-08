package pl.arekbednarz.gameshopapi.api.controller;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import pl.arekbednarz.gameshopapi.dto.ErrorDto;
import pl.arekbednarz.gameshopapi.dto.GameDto;
import pl.arekbednarz.gameshopapi.api.mapper.GameMapper;
import pl.arekbednarz.gameshopapi.api.service.CacheService;
import pl.arekbednarz.gameshopapi.api.service.GameService;

import java.util.List;

@Tag(name = "games")
@RestController
@RequestMapping(path = "api/v1/games")
public class GamesController {

    @Autowired
    private GameService gameService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private GameMapper gameMapper;

    @GetMapping(path = "list")
    @Operation(summary = "List of all games from database", description = "Get list of all games stored inside internal database")
    @APIResponse(responseCode = "200", description = "Success")
    @APIResponse(responseCode = "400", description = "Invalid data provided", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error")
    public List<GameDto> getGames(){
        final var games = gameService.getAllGames();

        return games
            .stream()
            .map(gameMapper::toDto)
            .toList();
    }

    @GetMapping(path = "list/comming/nextWeek")
    @Operation(summary = "List of all games comming in next 7 days", description = "List of all games comming in next 7 days stored in cache")
    @APIResponse(responseCode = "200", description = "Success")
    @APIResponse(responseCode = "400",description = "Invalid data provided")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public List<GameDto> getGamesCommingInNextWeek(){
        return gameService.getAllUpcommingNextWeek();
    }


    @GetMapping(path = "list/comming/nextMonth")
    @Operation(summary = "List of all games comming in next 30 days", description = "List of all games comming in next 30 days stored in cache")
    @APIResponse(responseCode = "200", description = "Success")
    @APIResponse(responseCode = "400",description = "Invalid data provided")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public List<GameDto> getGamesCommingInNextMonth(){
        return gameService.getAllUpcommingNextMonth();
    }


    @DeleteMapping
    @Operation(summary = "Delete games in cache", description = "Delete all games stored inside cache.")
    @APIResponse(responseCode = "200", description = "Games from cache deleted")
    @APIResponse(responseCode = "400", description = "Invalid data provided", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "Entity not found", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity deleteAllGamesInCache(){
        cacheService.clearCacheGamesNextWeek();
        cacheService.clearCacheGamesNextMonth();
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }



    @PutMapping("process-update")
    @Operation(summary = "Execute games data processing", description = "Download, and store games comming in next month into database.")
    @APIResponse(responseCode = "204", description = "Games processed")
    @APIResponse(responseCode = "400", description = "Invalid data provided", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "Entity not found", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error")
    public void runGamesDataProcessing() {
        gameService.rawgApiGamesListProcessing();
    }
}
