package com.gmpatter.shorty.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

@ApplicationPath("/shorty/v1")
public class ShortyV1Application extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return Set.of(UrlEndpoints.class);
  }
}
