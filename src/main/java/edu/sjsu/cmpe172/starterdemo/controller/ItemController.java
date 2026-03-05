package edu.sjsu.cmpe172.starterdemo.controller;

import edu.sjsu.cmpe172.starterdemo.model.Item;
import edu.sjsu.cmpe172.starterdemo.service.ItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<Item> getItems() {
        return service.getAllItems();
    }

    @PostMapping
    public Item createItem(@RequestBody Item item) {
        return service.addItem(item);
    }
}
