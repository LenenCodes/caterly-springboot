package com.caters.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.caters.dto.CatererProfileDto;
import com.caters.entity.Caterers;
import com.caters.entity.Users;
import com.caters.repository.CaterersRepository;
import com.caters.repository.UsersRepository;

@Service
public class CatererServiceImpl implements CatererService {

	private final CaterersRepository caterersRepository;
	private final UsersRepository usersRepository;

	@Value("${file.upload-dir:uploads/videos/}")
	private String uploadDir;

	@Value("${file.caterer-image-dir:uploads/caterer-images/}")
	private String catererImageDir;

	public CatererServiceImpl(CaterersRepository caterersRepository, UsersRepository usersRepository) {

		this.caterersRepository = caterersRepository;
		this.usersRepository = usersRepository;
	}

	@Override
	public Caterers getCatererByUser(Users user) {
		return caterersRepository.findByUser(user)
				.orElseThrow(() -> new RuntimeException("Caterer profile not found for user: " + user.getEmail()));
	}

	@Override
	public Caterers getCatererByEmail(String email) {

		Users user = usersRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User account not found with email: " + email));

		return getCatererByUser(user);
	}

	@Override
	public Optional<Caterers> getCatererById(Long id) {
		return caterersRepository.findById(id);
	}

	@Override
	public List<Caterers> getAllVerifiedCaterers() {
		return caterersRepository.findByIsVerifiedTrue();
	}

	@Override
	public List<Caterers> getAllPendingCaterers() {
		return caterersRepository.findByIsVerifiedFalse();
	}

	@Override
	@Transactional
	public void verifyCaterer(Long catererId) {

		Caterers caterer = caterersRepository.findById(catererId)
				.orElseThrow(() -> new RuntimeException("Caterer not found with ID: " + catererId));

		caterer.setVerified(true);

		caterersRepository.save(caterer);
	}

	@Override
	@Transactional
	public Caterers updateCatererProfile(Long catererId, CatererProfileDto dto) throws IOException {

		Caterers caterer = caterersRepository.findById(catererId)
				.orElseThrow(() -> new RuntimeException("Caterer not found with ID: " + catererId));

		// ==========================================
		// BASIC PROFILE INFORMATION
		// ==========================================

		caterer.setBusinessName(dto.getBusinessName());
		caterer.setPhone(dto.getPhone());
		caterer.setAddress(dto.getAddress());
		caterer.setDescription(dto.getDescription());

		// ==========================================
		// PROFILE IMAGE UPLOAD
		// ==========================================

		MultipartFile profileImage = dto.getProfileImage();

		if (profileImage != null && !profileImage.isEmpty()) {

			File directory = new File(catererImageDir);

			if (!directory.exists()) {
				directory.mkdirs();
			}

			String originalFileName = profileImage.getOriginalFilename();

			String extension = "";

			if (originalFileName != null && originalFileName.contains(".")) {

				extension = originalFileName.substring(originalFileName.lastIndexOf("."));
			}

			String uniqueFileName = "caterer_" + catererId + "_" + UUID.randomUUID() + extension;

			Path targetPath = Paths.get(catererImageDir).resolve(uniqueFileName);

			Files.copy(profileImage.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

			// Store the web-accessible path in database
			caterer.setProfileImageUrl("/uploads/caterer-images/" + uniqueFileName);
		}

		// ==========================================
		// EXTERNAL VIDEO URL
		// ==========================================

		if (dto.getVideoUrl() != null && !dto.getVideoUrl().trim().isEmpty()) {

			caterer.setVideoUrl(formatVideoEmbedUrl(dto.getVideoUrl().trim()));

		} else {

			caterer.setVideoUrl(null);
		}

		// ==========================================
		// DIRECT VIDEO FILE UPLOAD
		// ==========================================

		MultipartFile videoFile = dto.getVideoFile();

		if (videoFile != null && !videoFile.isEmpty()) {

			File directory = new File(uploadDir);

			if (!directory.exists()) {
				directory.mkdirs();
			}

			String originalFileName = videoFile.getOriginalFilename();

			String extension = "";

			if (originalFileName != null && originalFileName.contains(".")) {

				extension = originalFileName.substring(originalFileName.lastIndexOf("."));
			}

			String uniqueFileName = "caterer_" + catererId + "_" + UUID.randomUUID() + extension;

			Path targetPath = Paths.get(uploadDir).resolve(uniqueFileName);

			Files.copy(videoFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

			caterer.setVideoFilePath("/uploads/videos/" + uniqueFileName);
		}

		return caterersRepository.save(caterer);
	}

	/**
	 * Converts standard YouTube watch links into embeddable URLs.
	 */
	private String formatVideoEmbedUrl(String url) {

		if (url.contains("youtube.com/watch?v=")) {

			return url.replace("watch?v=", "embed/");

		} else if (url.contains("youtu.be/")) {

			return url.replace("youtu.be/", "www.youtube.com/embed/");
		}

		return url;
	}
}