package com.motion;

import jakarta.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import liqp.Template;
import org.springframework.cache.annotation.Cacheable;

@Singleton
public class TestLiqp {

  public static void main(String[] args) throws IOException {
    TestLiqp testLiqp = new TestLiqp();
    File file = testLiqp.getFile("liquid/test.liquid");
    testLiqp.getFile("liquid/test.liquid");

    //Read file
    String lines = Files.readString(file.toPath());

    Template template = Template.parse(lines);
    String rendered = template.render("name", "tobi");
    System.out.println(rendered);
  }

  @Cacheable("files")
  public File getFile(String fileName) throws IOException
  {
    System.out.println("oi");
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(fileName);

    if (resource == null) {
      throw new IllegalArgumentException("file is not found!");
    } else {
      return new File(resource.getFile());
    }
  }
}
