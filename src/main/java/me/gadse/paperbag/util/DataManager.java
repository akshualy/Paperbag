package me.gadse.paperbag.util;

import lombok.Getter;

import java.util.*;

public class DataManager {

    @Getter
    private final Set<UUID> openInventories = new HashSet<>();
}
