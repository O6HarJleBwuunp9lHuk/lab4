package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.*;
import org.example.service.UserService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management API", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя в системе")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Неверные данные пользователя"),
        @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    @PostMapping
    public ResponseEntity<UserDto> createUser(
        @Parameter(description = "Данные для создания пользователя", required = true)
        @Valid @RequestBody CreateUserRequest request) {

        UserDto user = userService.createUser(request);

        user.add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel());
        user.add(linkTo(methodOn(UserController.class).updateUser(user.getId(), new UpdateUserRequest())).withRel("update"));
        user.add(linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete"));
        user.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel(IanaLinkRelations.COLLECTION));

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список пользователей получен")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();

        for (UserDto user : users) {
            user.add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel());
            user.add(linkTo(methodOn(UserController.class).updateUser(user.getId(), new UpdateUserRequest())).withRel("update"));
            user.add(linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete"));
        }

        CollectionModel<UserDto> collectionModel = CollectionModel.of(users);
        collectionModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
        collectionModel.add(linkTo(methodOn(UserController.class).createUser(new CreateUserRequest())).withRel("create"));

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает пользователя по указанному ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователь найден",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
        @Parameter(description = "ID пользователя", required = true, example = "1")
        @PathVariable Long id) {

        UserDto user = userService.getUserById(id);

        user.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        user.add(linkTo(methodOn(UserController.class).updateUser(id, new UpdateUserRequest())).withRel("update"));
        user.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        user.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel(IanaLinkRelations.COLLECTION));

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Обновить пользователя", description = "Обновляет данные пользователя по ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Неверные данные для обновления"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
        @Parameter(description = "ID пользователя", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(description = "Данные для обновления", required = true)
        @Valid @RequestBody UpdateUserRequest request) {

        UserDto user = userService.updateUser(id, request);

        user.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        user.add(linkTo(methodOn(UserController.class).updateUser(id, request)).withRel("update"));
        user.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        user.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel(IanaLinkRelations.COLLECTION));

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteUser(
        @Parameter(description = "ID пользователя", required = true, example = "1")
        @PathVariable Long id) {

        userService.deleteUser(id);

        Response response = new Response("Пользователь успешно удален", "DELETED");
        response.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel(IanaLinkRelations.COLLECTION));
        response.add(linkTo(methodOn(UserController.class).createUser(new CreateUserRequest())).withRel("create"));

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Проверить существование пользователя", description = "Проверяет существует ли пользователь с указанным email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Проверка выполнена")
    })
    @GetMapping("/exists")
    public ResponseEntity<Response> checkUserExists(
        @Parameter(description = "Email для проверки", required = true, example = "user@example.com")
        @RequestParam String email) {

        boolean exists = userService.userExists(email);

        Response response = new Response(
            exists ? "Пользователь существует" : "Пользователь не существует",
            exists ? "EXISTS" : "NOT_FOUND"
        );

        response.add(linkTo(methodOn(UserController.class).checkUserExists(email)).withSelfRel());
        response.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        if (!exists) {
            response.add(linkTo(methodOn(UserController.class).createUser(new CreateUserRequest())).withRel("create"));
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Информация о User API", description = "Возвращает основную информацию о User Management API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Информация получена")
    })
    @GetMapping("/info")
    public ResponseEntity<Response> getApiInfo() {
        Response response = new Response("User Management API is running", "ACTIVE");

        response.add(linkTo(methodOn(UserController.class).getApiInfo()).withSelfRel());
        response.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        response.add(linkTo(methodOn(UserController.class).createUser(new CreateUserRequest())).withRel("create-user"));

        return ResponseEntity.ok(response);
    }
}
