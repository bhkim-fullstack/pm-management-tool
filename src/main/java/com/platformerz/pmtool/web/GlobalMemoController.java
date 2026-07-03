package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.GlobalMemo;
import com.platformerz.pmtool.repository.GlobalMemoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/global-memo")
public class GlobalMemoController {

	private final GlobalMemoRepository globalMemoRepository;

	public GlobalMemoController(GlobalMemoRepository globalMemoRepository) {
		this.globalMemoRepository = globalMemoRepository;
	}

	@GetMapping
	public MemoResponse get() {
		return globalMemoRepository.findById(GlobalMemo.SINGLETON_ID)
			.map(memo -> new MemoResponse(memo.getContent()))
			.orElseGet(() -> new MemoResponse(""));
	}

	@PutMapping
	public MemoResponse update(@RequestBody MemoRequest request) {
		GlobalMemo memo = globalMemoRepository.findById(GlobalMemo.SINGLETON_ID).orElseGet(GlobalMemo::new);
		memo.setContent(request.content());
		globalMemoRepository.save(memo);
		return new MemoResponse(memo.getContent());
	}

}
