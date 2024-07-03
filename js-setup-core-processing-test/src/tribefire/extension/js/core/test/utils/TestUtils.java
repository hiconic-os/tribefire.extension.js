// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package tribefire.extension.js.core.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;

public class TestUtils {

	/**
	 * @param file - a single file or a directory
	 */
	public static void delete( File file) {
		if (file == null || file.exists() == false)
			return;
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				if (child.isDirectory()) {
					delete( child);
				} 
				child.delete();			
			}
		}		
		file.delete();		
	}
	
	/**
	 * @param output - the directory to ensure it's there and empty
	 */
	public static void ensure(File output) {	
		if (output.exists())
			delete( output);
		output.mkdirs();
	}
	
	/**
	 * copies a file or a directory to another file or directory 
	 * @param source
	 * @param target
	 */
	public static void copy(File source, File target){
		try {
			FileTools.copyFileOrDirectory(source, target);
		} catch (Exception e) {
			throw new IllegalStateException("cannot copy [" + source.getAbsolutePath() + "] to [" + target.getAbsolutePath() + "]", e);
		}
	}
	public static String generateHash(File sourceFile, String alg) {
		return generateHash(sourceFile, Collections.singletonList(alg)).get(alg);
	}
	
	public static Map<String, String> generateHash(File sourceFile, List<String> types) {
		Map<String, String> result = new HashMap<>();
		List<MessageDigest> digests = types.stream().map( t -> {
			try {
				return MessageDigest.getInstance( t);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("no digest found for [" + t + "]");
			}
		}).collect( Collectors.toList());
	
		byte bytes [] = new byte[65536];
		try (FileInputStream in = new FileInputStream( sourceFile)) {
			int size = 0;
			while ((size = in.read(bytes)) != -1) {
				for (MessageDigest digest : digests)  {
					digest.update(bytes, 0, size);
				}				
			}
			for (int i = 0; i < types.size(); i++)  {
				MessageDigest digest = digests.get( i);
				byte [] digested = digest.digest();
				result.put( types.get(i), StringTools.toHex(digested));
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException("can't read [" + sourceFile.getAbsolutePath() + "]", e);
		}
		return result;
	}
}
