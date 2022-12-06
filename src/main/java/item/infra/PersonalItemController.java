package item.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import item.model.PersonalItem;
import item.service.PersonalItemService;
import org.eclipse.jetty.http.HttpStatus;
import shared.service.JsonSerializer;
import spark.Service;

public class PersonalItemController {
    private PersonalItemService personalItemService;

    public PersonalItemController(Boolean isTest) {
        if(isTest) {
            personalItemService = new PersonalItemService(new PersonalItemInMemoryRepository(isTest));
        } else {
            // TODO noTestVersion???
            personalItemService = new PersonalItemService(new PersonalItemInMemoryRepository(isTest));
        }
    }

    public void createRoutes(Service server) {
        JsonSerializer jsonSerializer = new JsonSerializer();

        server.get("/personal/items", "*",
                (request, response) -> {
                    return personalItemService.all();
                },
                jsonSerializer::serialize);

        server.get("/personal/items/:id", (request, response) -> {
            long id = Long.parseLong(request.params("id"));
            return personalItemService.getById(id);
        }, jsonSerializer::serialize);

        server.post("/personal/items", (request, response) -> {
            PersonalItem item = jsonSerializer.deserialize(request.body(), new TypeReference<PersonalItem>() {});
            System.out.println("POST: " + item);
            if(item.getId()!=null) {
                // update
                System.out.println("UPDATE: " + item);
                response.status(HttpStatus.ACCEPTED_202);
                return personalItemService.update(item);
            } else {
                System.out.println("CREATE: " + item);
                response.status(HttpStatus.CREATED_201);
                return personalItemService.create(item);
            }
        }, jsonSerializer::serialize);

    }
}
