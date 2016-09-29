package com.avinetworks.docs.web;

import com.avinetworks.docs.deploy.Pusher;
import com.avinetworks.docs.deploy.Renderer;
import com.avinetworks.docs.deploy.Repository;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static spark.Spark.*;

public class PushHandler {
  private static final File OUTDIR = new File(System.getProperty("user.home"), ".avi-docs-repo");
  private static final Executor executor = Executors.newSingleThreadExecutor();
  private Repository repo;
  private Renderer renderer;
  private Pusher pusher;

  private PushHandler(final Repository repo, final Renderer renderer, Pusher pusher) {
    this.repo = repo;
    this.renderer = renderer;
    this.pusher = pusher;
  }

  private Object doGet() throws IOException {
    executor.execute(() -> {
      try {
        repo.cloneOrPull();
        renderer.execute();
        pusher.execute();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    return "Success!";
  }

  public static void main(String[] args) throws IOException {
    if (!OUTDIR.isDirectory()) {
      FileUtils.forceMkdir(OUTDIR);
      if (!OUTDIR.isDirectory()) {
        throw new RuntimeException("Can't create output directory: " + OUTDIR);
      }
    }
    final Repository repo = new Repository();
    final Renderer renderer = new Renderer();
    final Pusher pusher = new Pusher();
    get("/helper/push", (req, res) -> new PushHandler(repo, renderer, pusher).doGet());
  }

}
