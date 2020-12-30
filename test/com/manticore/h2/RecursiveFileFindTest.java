/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.manticore.h2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;

/** @author are */
public class RecursiveFileFindTest {
	public static final Logger LOGGER = Logger.getLogger(RecursiveFileFindTest.class.getName());
	
	@Test
	public void findH2FilesInHome() {
		try {
			String homeFolderStr = H2MigrationTool.getAbsoluteFileName("~");
			LOGGER.info(homeFolderStr);
			
			for (Path p: H2MigrationTool.findH2Databases(homeFolderStr)) {
				LOGGER.info(p.toString());
			}
		} catch (IOException ex) {
			Logger.getLogger(RecursiveFileFindTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
