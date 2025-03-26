package com.example.demo;

import com.example.demo.model.QRCode;
import com.example.demo.repository.QRCodeRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RestController
public class QrCodeGeneratorApplication {

	@Autowired
	private QRCodeRepository qrCodeRepository;

	// Endpoint to generate and download QR code
	@GetMapping("/generate")
	public void generateQRCode(
			@RequestParam String data,
			@RequestParam(defaultValue = "300") int width,
			@RequestParam(defaultValue = "300") int height,
			HttpServletResponse response) throws IOException, WriterException {

		// Set QR code encoding hints
		Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		hints.put(EncodeHintType.MARGIN, 1);

		// Generate QR code
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);

		// Convert BitMatrix to PNG image
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

		// Set response headers to return the image
		response.setContentType(MediaType.IMAGE_PNG_VALUE);
		response.getOutputStream().write(outputStream.toByteArray());
		response.getOutputStream().flush();

		// Save the QR code to the database
		QRCode qrCode = new QRCode();
		qrCode.setUrl(data);
		qrCodeRepository.save(qrCode);
	}
	@PostMapping("/save-qr-code")
	public void saveQRCode(@RequestBody QRCode qrCode) {
		qrCodeRepository.save(qrCode);  // Save the QR code to the database
	}
	// Endpoint to view all saved QR codes
	@GetMapping("/view-qr-codes")
	public List<QRCode> viewQRCodeHistory() {
		// Retrieve all saved QR codes from the database
		return qrCodeRepository.findAll();
	}
	@DeleteMapping("/delete-qr-code/{id}")
	public ResponseEntity<Map<String, String>> deleteQRCode(@PathVariable Long id) {
		// Check if the QR code exists
		if (qrCodeRepository.existsById(id)) {
			qrCodeRepository.deleteById(id);  // Delete the QR code from the database
			Map<String, String> response = new HashMap<>();
			response.put("message", "QR Code deleted successfully");

			return ResponseEntity.ok(response);  // Return success message as JSON
		} else {
			Map<String, String> response = new HashMap<>();
			response.put("message", "QR Code not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);  // Return error message as JSON
		}
	}

//test

	public static void main(String[] args) {
		SpringApplication.run(QrCodeGeneratorApplication.class, args);
	}
}
