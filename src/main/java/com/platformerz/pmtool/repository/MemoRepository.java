package com.platformerz.pmtool.repository;

import com.platformerz.pmtool.domain.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoRepository extends JpaRepository<Memo, Long> {
}
