package com.platformerz.pmtool.repository;

import com.platformerz.pmtool.domain.GlobalMemo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlobalMemoRepository extends JpaRepository<GlobalMemo, Long> {
}
