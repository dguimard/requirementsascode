package shoppingappextract.extract;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.requirementsascode.Model;
import org.requirementsascode.extract.freemarker.FreeMarkerEngine;

import shoppingapp.boundary.Boundary;

public class ShoppingAppExtract {
	public static void main(String[] args) throws Exception {
		new ShoppingAppExtract().start();
	}

	private void start() throws Exception {
		Model model = buildModel();

		FreeMarkerEngine engine = new FreeMarkerEngine("shoppingappextract/extract");
		File outputFile = outputFile();
		engine.extract(model, templateFileName(), new FileWriter(outputFile));

		System.out.println("Wrote file to: " + outputFile);
	}

	private Model buildModel() {
		Boundary boundary = new Boundary(null, null);
		Model model = boundary.getModel();
		return model;
	}

	private String templateFileName() {
		return "htmlExample.ftlh";
	}

	private File outputFile() throws IOException {
		File outputFile = File.createTempFile("shoppingappextract_", ".html");
		return outputFile;
	}
}
