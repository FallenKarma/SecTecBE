package it.sectec.platform.storage.controller;

import it.sectec.platform.storage.dto.FileAssetDto.*;
import it.sectec.platform.storage.entity.FileAsset;
import it.sectec.platform.storage.service.FileAssetService;
import it.sectec.platform.shared.crud.CrudController;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/file-assets")
public class FileAssetController extends CrudController<FileAsset, Request, Response> {
    public FileAssetController(FileAssetService service) { super(service); }
    @Override
    protected UUID id(Response response) { return response.id(); }
}

