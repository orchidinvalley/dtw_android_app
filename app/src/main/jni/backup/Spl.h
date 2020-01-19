/*
 * Spl.h
 *
 *  Created on: 2019Äê12ÔÂ26ÈÕ
 *      Author: Administrator
 */

#ifndef SRC_SPH_H_
#define SRC_SPH_H_

void hanning( float *win, int N);
void hamming( float *win, int N);
void apply_window(float* data, float* window, int window_len);

#endif /* SRC_SPH_H_ */
