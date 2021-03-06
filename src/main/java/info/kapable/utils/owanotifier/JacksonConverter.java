/**
The MIT License (MIT)

Copyright (c) 2017 Mathieu GOULIN

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package info.kapable.utils.owanotifier;

import java.io.IOException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

import retrofit.mime.TypedInput;

/**
 * Converter to transform retrofit TypedInput to java Object
 */
@SuppressWarnings("rawtypes")
public class JacksonConverter implements Converter
{
	// the objectMapper field
	private final ObjectMapper objectMapper;

	/**
	 * Update the objectMapper field
	 * 
	 * @param objectMapper
	 *            The new object Mapper
	 */
	public JacksonConverter(ObjectMapper objectMapper)
	{
		this.objectMapper = objectMapper;
	}

	/**
	 * Use objectMapper to convert TypedInput to object of specific class
	 * 
	 * @param body
	 *            The serialized object
	 * @param type
	 *            Type of object to return
	 * @return An unserialized java object
	 * @throws JsonParseException
	 *             In case of Exception durring parsing
	 * @throws JsonMappingException
	 *             In case of body is not correct Json
	 * @throws IOException
	 *             In case of IOException
	 */
	public Object fromBody(TypedInput body, Type type) throws JsonParseException, JsonMappingException, IOException
	{
		JavaType javaType = objectMapper.getTypeFactory().constructType(type);
		return objectMapper.readValue(body.in(), javaType);
	}

	@Override
	public Object convert(Object arg0)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public JavaType getInputType(TypeFactory arg0)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public JavaType getOutputType(TypeFactory arg0)
	{
		throw new UnsupportedOperationException("Not implemented");
	}
}