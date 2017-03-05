package net.mfjassociates.tools;

import static net.mfjassociates.tools.ClasspathHelper.DEFAULT_WORKBOOK_FILE;
import static net.mfjassociates.tools.ClasspathHelper.openWorkbook;
import static net.mfjassociates.tools.ClasspathHelper.storeClasspathToExcel;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

public class ClasspathHelperTest {

	private static final String JUNIT_SHEET_NAME = "JUNIT-TEST";
	@Test
	public void testStoreClasspathToExcel() throws IOException {
		storeClasspathToExcel(JUNIT_SHEET_NAME);
		assertTrue(DEFAULT_WORKBOOK_FILE.getAbsolutePath()+" workbook does not exist", DEFAULT_WORKBOOK_FILE.exists());
		Workbook wb=openWorkbook(DEFAULT_WORKBOOK_FILE);
		assertNotNull("worksheet "+JUNIT_SHEET_NAME+" does not exist",wb.getSheet(JUNIT_SHEET_NAME));
		assertEquals("first cell is incorrect", "Name", wb.getSheet(JUNIT_SHEET_NAME).getRow(0).getCell(0).getStringCellValue());
	}
	
	@Test
	public void testDuplicateSheet() throws IOException {
		storeClasspathToExcel(JUNIT_SHEET_NAME);
		storeClasspathToExcel(JUNIT_SHEET_NAME);
	}

}
