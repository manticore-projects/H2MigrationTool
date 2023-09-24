/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.manticore.h2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * @author are
 */
public class RecursiveFileFindTest {

    public static final Logger LOGGER = Logger.getLogger(RecursiveFileFindTest.class.getName());

    @Test
    public void findH2FilesInTestResources() {
        Assertions.assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                String homeFolderStr = H2MigrationTool.getAbsoluteFileName("src/test");
                LOGGER.info(homeFolderStr);
                for (Path p : H2MigrationTool.findH2Databases(homeFolderStr)) {
                    LOGGER.info(p.toString());
                }
            }
        });
    }
}
