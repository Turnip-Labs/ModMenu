package io.github.prospector.modmenu.util;


import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.*;
import net.fabricmc.loader.util.version.VersionParsingException;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TestModContainer implements ModContainer {

	public static final Random RAND = new Random();
	private static Collection<ModContainer> testModContainers;

	public static Collection<ModContainer> getTestModContainers() {
		if (testModContainers == null) {
			testModContainers = new ArrayList<>();
			for (int i = 0; i < 1000; i++) {
				testModContainers.add(new TestModContainer());
			}
		}
		return testModContainers;
	}

	private final ModMetadata metadata = new TestModMetadata();
	private final Path rootPath = FabricLoader.getInstance().getModContainer("fabricloader").orElseThrow(IllegalStateException::new).getRootPath();

	@Override
	public ModMetadata getMetadata() {
		return this.metadata;
	}

    @Override
    public List<Path> getRootPaths() {
        return null;
    }

    @Override
    public ModOrigin getOrigin() {
        return null;
    }

    @Override
    public Optional<ModContainer> getContainingMod() {
        return Optional.empty();
    }

    @Override
    public Collection<ModContainer> getContainedMods() {
        return null;
    }

    @Override
	public Path getRootPath() {
		return this.rootPath;
	}

    @Override
    public Path getPath(String file) {
        return null;
    }

    private static String randomAlphabetic(int minLen, int maxLen) {
		int len = ThreadLocalRandom.current().nextInt(maxLen - minLen + 1) + minLen;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			int rand = ThreadLocalRandom.current().nextInt(26 * 2);
			if (rand < 26)
				sb.append((char) ('A' + rand));
			else
				sb.append((char) ('a' - 26 + rand));
		}
		return sb.toString();
	}

	private static String randomAlphanumeric(int minLen, int maxLen) {
		int len = ThreadLocalRandom.current().nextInt(maxLen - minLen + 1) + minLen;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			int rand = ThreadLocalRandom.current().nextInt(26 * 2 + 10);
			if (rand < 26)
				sb.append((char) ('A' + rand));
			else if (rand < 26 * 2)
				sb.append((char) ('a' - 26 + rand));
			else
				sb.append((char) ('0' - (26 * 2) + rand));
		}
		return sb.toString();
	}

	public static class TestModMetadata implements ModMetadata {
		private final String id;
		private final String description;
		private final Version version;

		public TestModMetadata() {
			super();
			this.id = randomAlphabetic(10, 50).toLowerCase(Locale.ROOT);
			this.description = randomAlphabetic(0, 500);
			try {
				this.version = SemanticVersion.parse(String.format("%d.%d.%d+%s", RAND.nextInt(10), RAND.nextInt(50), RAND.nextInt(200), randomAlphanumeric(2, 10)));
			} catch (VersionParsingException e) {
				throw new AssertionError("Generated version is not semantic", e);
			}
		}

		@Override
		public String getType() {
			return "test";
		}

		@Override
		public String getId() {
			return this.id;
		}

		@Override
		public Collection<String> getProvides() {
			return null;
		}

		@Override
		public Version getVersion() {
			return this.version;
		}

		@Override
		public ModEnvironment getEnvironment() {
			return null;
		}

        @Override
        public Collection<ModDependency> getDependencies() {
            return null;
        }

        @Override
		public Collection<ModDependency> getDepends() {
			return Collections.emptyList();
		}

		@Override
		public Collection<ModDependency> getRecommends() {
			return Collections.emptyList();
		}

		@Override
		public Collection<ModDependency> getSuggests() {
			return Collections.emptyList();
		}

		@Override
		public Collection<ModDependency> getConflicts() {
			return Collections.emptyList();
		}

		@Override
		public Collection<ModDependency> getBreaks() {
			return Collections.emptyList();
		}

		@Override
		public String getName() {
			return this.getId();
		}

		@Override
		public String getDescription() {
			return this.description;
		}

		@Override
		public Collection<Person> getAuthors() {
			return Collections.emptyList();
		}

		@Override
		public Collection<Person> getContributors() {
			return Collections.emptyList();
		}

		@Override
		public ContactInformation getContact() {
			return ContactInformation.EMPTY;
		}

		@Override
		public Collection<String> getLicense() {
			return Collections.emptyList();
		}

		@Override
		public Optional<String> getIconPath(int size) {
			return Optional.empty();
		}

		@Override
		public boolean containsCustomValue(String key) {
			return false;
		}

		@Override
		public CustomValue getCustomValue(String key) {
			return null;
		}

		@Override
		public Map<String, CustomValue> getCustomValues() {
			return new HashMap<>();
		}

        @SuppressWarnings("UnstableApiUsage")
		@Override
        public boolean containsCustomElement(String key) {
            return false;
        }
    }
}
