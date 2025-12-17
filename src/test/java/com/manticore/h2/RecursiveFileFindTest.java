/*
 * H2MigrationTool is a Graphical User Application for Recovering and Migration H2 Database files.
 *
 * Copyright (C) 2020-2023 Andreas Reichel<andreas@manticore-projects.com>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
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
