package edu.sjsu.cmpe172.salon.service;

import edu.sjsu.cmpe172.salon.model.Item;
import edu.sjsu.cmpe172.salon.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {

    private final ItemRepository repo;

    public ItemService(ItemRepository repo) {
        this.repo = repo;
    }

    public List<Item> getAllItems() {
        return repo.findAll();
    }

    public Item addItem(Item item) {
        return repo.save(item);
    }
}
