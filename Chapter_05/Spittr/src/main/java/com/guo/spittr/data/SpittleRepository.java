package com.guo.spittr.data;

import com.guo.spittr.Spittle;

import java.util.List;

/**
 * Created by guo on 24/2/2018.
 */
public interface SpittleRepository {
    List<Spittle> findSpittles(long max, int count);
}
