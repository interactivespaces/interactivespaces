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

package interactivespaces.service.web.server.handler.barcode;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.web.server.HttpDynamicRequestHandler;
import interactivespaces.service.web.server.HttpRequest;
import interactivespaces.service.web.server.HttpResponse;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Writer;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

/**
 * A barcode HTTP request handler based on zxing.
 * 
 * @author Keith M. Hughes
 */
public class ZXingBarcodeHttpDynamicRequestHandler implements
		HttpDynamicRequestHandler {

	/**
	 * Query string parameter name for the content to be displayed.
	 */
	public static final String QUERY_PARAMETER_NAME_CONTENT = "content";

	@Override
	public void handle(HttpRequest request, HttpResponse response) {
		String barcodeContent = getBarcodeContent(request);
		if (barcodeContent == null) {
			throw new InteractiveSpacesException(String.format(
					"No content for barcode generator for URL %s",
					request.getUri()));
		}

		try {
			Charset charset = Charset.forName("UTF-8");
			CharsetEncoder encoder = charset.newEncoder();
			byte[] b = null;
			// Convert a string to UTF-8 bytes in a ByteBuffer
			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(barcodeContent));
			b = bbuf.array();

			String data = new String(b, "UTF-8");
			// get a byte matrix for the data
			BitMatrix matrix = null;
			int h = 100;
			int w = 100;
			Writer writer = new MultiFormatWriter();

			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>(
					2);
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			matrix = writer.encode(data, BarcodeFormat.QR_CODE, w, h, hints);

			MatrixToImageWriter.writeToStream(matrix, "PNG",
					response.getOutputStream());
		} catch (Exception e) {
			throw new InteractiveSpacesException(
					"Unable to generate barcode message", e);
		}

	}

	/**
	 * Get the barcode content.
	 * 
	 * <p>
	 * The default implementation will look at the URL and get the query
	 * parameter with name {@link #QUERY_PARAMETER_NAME_CONTENT}.
	 * 
	 * <p>
	 * If other behavior is wanted, override this method in a subclass.
	 * 
	 * @param request
	 *            the HTTP request
	 * 
	 * @return the content to be displayed in the barcode
	 */
	public String getBarcodeContent(HttpRequest request) {
		String barcodeContent = request.getUriQueryParameters().get(
				QUERY_PARAMETER_NAME_CONTENT);

		return barcodeContent;
	}
}
