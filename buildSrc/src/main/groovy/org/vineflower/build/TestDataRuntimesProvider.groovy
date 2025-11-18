package org.vineflower.build

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Nested
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.process.CommandLineArgumentProvider

import javax.inject.Inject

public class TestDataRuntimesProvider implements CommandLineArgumentProvider {
  @Nested
  final MapProperty<String, JavaLauncher> launchers

  @Inject
  public TestDataRuntimesProvider(final ObjectFactory objects) {
    this.launchers = objects.mapProperty(Integer, JavaLauncher)
  }

  @Override
  Iterable<String> asArguments() {
    def result = []
    this.launchers.get().each { k, v ->
      result << "-Djava.${k}.home=${v.metadata.installationPath.asFile.absolutePath}"
    }
    return result
  }
}