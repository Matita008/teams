package io.matita08.plugins.teams.storage;

import java.util.Map;

public interface Savable<T> {
   Map<String, String> getSaveData();
   T getObject(Map<String, String> saveData);
}
