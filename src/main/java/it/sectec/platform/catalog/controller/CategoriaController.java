package it.sectec.platform.catalog.controller;

import it.sectec.platform.catalog.dto.CategoriaDto.*;
import it.sectec.platform.catalog.entity.Categoria;
import it.sectec.platform.catalog.service.CategoriaService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categorie")
public class CategoriaController extends CrudController<Categoria, Request, Response> {
    public CategoriaController(CategoriaService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

