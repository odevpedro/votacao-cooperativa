package com.example.cooperativevoting.shared.api;

public final class ApiRoutes {

  private ApiRoutes() {}

  public static final String API_V1 = "/api/v1";

  public static final class Agendas {
    public static final String ROOT = API_V1 + "/agendas";
    public static final String BY_ID = ROOT + "/{agendaId}";
    public static final String SESSIONS = BY_ID + "/sessions";
    public static final String CURRENT_SESSION = SESSIONS + "/current";
    public static final String VOTES = BY_ID + "/votes";
    public static final String RESULTS = BY_ID + "/results";

    private Agendas() {}
  }

  public static final class Mobile {
    public static final String ROOT = API_V1 + "/mobile";
    public static final String AGENDAS = ROOT + "/agendas";
    public static final String IDENTIFY = AGENDAS + "/{agendaId}/identify";
    public static final String VOTE_OPTIONS = AGENDAS + "/{agendaId}/vote-options";

    private Mobile() {}
  }
}
