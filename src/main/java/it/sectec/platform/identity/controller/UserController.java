package it.sectec.platform.identity.controller;

import it.sectec.platform.identity.dto.UserDto;
import it.sectec.platform.identity.dto.UserDto.Request;
import it.sectec.platform.identity.dto.UserDto.Response;
import it.sectec.platform.identity.entity.User;
import it.sectec.platform.identity.service.UserService;
import it.sectec.platform.shared.crud.CrudController;
import it.sectec.platform.shared.crud.CrudService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController extends CrudController<User, Request, Response> {
    private final UserService utenti;
    public UserController(UserService service) {
        super(service);
        this.utenti = service;
    }
    @Override
    protected UUID id(Response response) { return response.id(); }

    @GetMapping("/search")
    public CrudService.PageResponse<UserDto.Response> search(@RequestParam(required = false) String nome,
                                                             @RequestParam(required = false) String cognome,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        return utenti.search(nome, cognome, page, size);
    }
}

