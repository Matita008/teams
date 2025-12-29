package io.matita08.plugins.teams.storage;

public class MissingDataException extends Exception {
   public MissingDataException(String message) {
      super(message);
   }
   
   public MissingDataException(String message, Throwable cause) {
      super(message, cause);
   }
}
