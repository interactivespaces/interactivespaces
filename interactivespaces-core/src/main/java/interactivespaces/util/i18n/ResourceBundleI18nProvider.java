/*
 * Copyright (C) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package interactivespaces.util.i18n;

import interactivespaces.InteractiveSpacesException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Use the Java ResourceBundle API to provide internationalization services.
 * 
 * <p>
 * Java resource bundles for internationalization use files with a particular
 * naming convention. The overall bundle has a prefix name giving the name of
 * the collection, e.g. {@code messages}. The extension will be
 * {@code .properties}. Next, the language code is used after the base. So, for
 * example {@code messages_fr.properties} will give the properties in French.
 * 
 * <p>
 * If you need country specific modifications, you then add the country code.
 * For example, for France, the file would be {@code messages_fr_FR.properties}.
 * 
 * <p>
 * There will be one of these files for each language, all contained in the same
 * folder.
 * 
 * <p>
 * A file with the name {@code messages.properties} will give default values
 * when a particular language cannot be found.
 * 
 * @author Keith M. Hughes
 */
public class ResourceBundleI18nProvider implements I18nProvider {

	/**
	 * The file extension used for the bundle files.
	 */
	private static final String BUNDLE_FILE_EXTENSION = "properties";

	/**
	 * base folder for finding the message bundles.
	 */
	private File baseFolder;

	/**
	 * base name for the bundle information.
	 */
	private String baseName;

	/**
	 * The bundle control used to get the bundles from the file system.
	 */
	private ResourceBundle.Control bundleControl;

	/**
	 * Construct a new internationalization provider
	 * 
	 * @param baseFolder
	 *            the folder that contains the internationalization files
	 * @param baseName
	 *            the base name for the group of files providing the
	 *            internationalized set of strings
	 */
	public ResourceBundleI18nProvider(final File baseFolder, String baseName) {
		this.baseFolder = baseFolder;
		this.baseName = baseName;

		bundleControl = new ResourceBundle.Control() {

			@Override
			public List<String> getFormats(String baseName) {
				if (baseName == null)
					throw new NullPointerException();
				return Lists.newArrayList(BUNDLE_FILE_EXTENSION);
			}

			@Override
			public ResourceBundle newBundle(String baseName, Locale locale,
					String format, ClassLoader loader, boolean reload)
					throws IllegalAccessException, InstantiationException,
					IOException {
				if (baseName == null || locale == null || format == null
						|| loader == null)
					throw new NullPointerException();
				ResourceBundle bundle = null;
				if (format.equals(BUNDLE_FILE_EXTENSION)) {
					String bundleName = toBundleName(baseName, locale);
					String resourceName = toResourceName(bundleName, format);

					File file = new File(baseFolder, resourceName);

					InputStream stream = null;
					if (reload) {
						if (file.exists()) {
							stream = new FileInputStream(file);
						}
					} else {
						stream = new FileInputStream(file);
					}
					if (stream != null) {
						InputStreamReader reader = new InputStreamReader(
								new BufferedInputStream(stream), "UTF-8");
						bundle = new PropertyResourceBundle(reader);
						reader.close();
					}
				}
				return bundle;
			}

		};
	}

	@Override
	public Set<Locale> getSupportedLocales() {
		Set<Locale> locales = Sets.newHashSet();

		// Only take the ones with the _ as these are the locale specific
		// entries.
		final String prefix = baseName + "_";
		for (File messageFile : baseFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(prefix)
						&& name.endsWith("." + BUNDLE_FILE_EXTENSION);
			}
		})) {
			int beginIndex = baseName.length() + 1;
			String fileName = messageFile.getName();
			String languageCode = new String(fileName.substring(beginIndex,
					beginIndex + 2));

			// Is there a country code?
			String countryCode = null;
			if (fileName.indexOf("_", beginIndex + 2) != -1) {
				countryCode = new String(fileName.substring(beginIndex + 3,
						beginIndex + 5));

				locales.add(new Locale(languageCode, countryCode));
			} else {
				locales.add(new Locale(languageCode));
			}
		}

		return locales;
	}

	@Override
	public Set<I18nSource> getAllSupportedSources() {
		Set<I18nSource> sources = Sets.newHashSet();

		for (Locale locale : getSupportedLocales()) {
			sources.add(getSource(locale));
		}

		return sources;
	}

	@Override
	public I18nSource getSource(Locale locale) {
		try {
			return new JavaI18nSource(ResourceBundle.getBundle(baseName,
					locale, bundleControl), locale);
		} catch (MissingResourceException e) {
			throw new InteractiveSpacesException(
					String.format(
							"Could not find any internationalization resources for %s in %s",
							baseName, baseFolder.getAbsolutePath()));
		}
	}

	/**
	 * Internationalization resource using a MessageBundle.
	 * 
	 * @author Keith M. Hughes
	 */
	private static class JavaI18nSource implements I18nSource {

		/**
		 * Resource bundle containing the data.
		 */
		private ResourceBundle bundle;

		/**
		 * The locale for this source.
		 */
		private Locale locale;

		/**
		 * The message formatter for this source.
		 */
		private MessageFormat formatter;

		public JavaI18nSource(ResourceBundle bundle, Locale locale) {
			this.bundle = bundle;
			this.locale = locale;

			formatter = new MessageFormat("");
			formatter.setLocale(locale);
		}

		@Override
		public String getMessage(String key) {
			try {
				return bundle.getString(key);
			} catch (MissingResourceException e) {
				return null;
			}
		}

		@Override
		public String getMessage(String messageKey, List<String> args) {
			String message = getMessage(messageKey);
			if (message != null) {
				formatter.applyPattern(message);
				return formatter.format(args.toArray());
			} else {
				return null;
			}
		}

		@Override
		public Locale getLocale() {
			return locale;
		}
	}
}
