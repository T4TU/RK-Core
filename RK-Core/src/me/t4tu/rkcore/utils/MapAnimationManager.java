package me.t4tu.rkcore.utils;

import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.t4tu.rkcore.Core;

public class MapAnimationManager {
	
	private short[] a1f0 = {
			0, 1, 2, 3, 4, 5, 6, 
			7, 8, 9, 10, 11, 12, 13, 
			14, 15, 16, 17, 18, 19, 20, 
			21, 22, 23, 24, 25, 26, 27, 
			28, 29, 30, 31, 32, 33, 34, 
			35, 36, 37, 38, 39, 40, 41, 
			42, 43, 44, 45, 46, 47, 48, 
			49, 50, 51, 52, 53, 54, 55, 
			56, 57, 58, 59, 60, 61, 62, 
			63, 64, 65, 66, 67, 68, 69, 
			70, 71, 72, 73, 74, 75, 76, 
			77, 78, 79, 80, 81, 82, 83, 
			84, 85, 86, 87, 88, 89, 90
			};
	private short[] a1f1 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, 148, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a1f2 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, 232, -1, -1, -1, -1, -1, 
			-1, 148, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a1f3 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, 323, 324, -1, -1, -1, -1, 
			-1, 148, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a1f4 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, 323, 415, -1, -1, -1, -1, 
			-1, 148, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a1f5 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, 323, 506, -1, -1, -1, -1, 
			-1, 148, 513, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a1f6 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, 323, 506, -1, -1, -1, -1, 
			-1, 148, 604, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a1f7 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, 323, 506, -1, -1, -1, -1, 
			-1, 148, 695, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a1f8 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, 323, 506, 780, -1, -1, -1, 
			-1, 148, 695, 787, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a1f9 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, 323, 506, 871, -1, -1, -1, 
			-1, 148, 695, 878, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a1f10 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, 323, 506, 962, 963, -1, -1, 
			-1, 148, 695, 878, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a1f11 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, 323, 506, 962, 1054, -1, -1, 
			-1, 148, 695, 878, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a1f12 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1138, -1, -1, 
			-1, 323, 506, 962, 1054, -1, -1, 
			-1, 148, 695, 878, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a1f13 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1229, -1, -1, 
			-1, 323, 506, 962, 1054, -1, -1, 
			-1, 148, 695, 878, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f0 = {
			0, 1, 2, 3, 4, 5, 6, 
			7, 8, 9, 10, 11, 12, 13, 
			14, 15, 16, 17, 18, 19, 20, 
			21, 22, 23, 24, 25, 26, 27, 
			28, 29, 30, 31, 32, 33, 34, 
			35, 36, 37, 38, 39, 40, 41, 
			42, 43, 44, 45, 46, 47, 48, 
			49, 50, 51, 52, 53, 54, 55, 
			56, 57, 58, 59, 60, 61, 62, 
			63, 64, 65, 66, 67, 68, 69, 
			70, 71, 72, 73, 74, 75, 76, 
			77, 78, 79, 80, 81, 82, 83, 
			84, 85, 86, 87, 88, 89, 90
			};
	private short[] a2f1 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1411, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f2 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1502, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f3 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1502, -1, -1, 
			-1, -1, -1, -1, 1600, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f4 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1502, -1, -1, 
			-1, -1, -1, 1690, 1691, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f5 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1502, -1, -1, 
			-1, -1, -1, 1781, 1691, -1, -1, 
			-1, -1, -1, 1788, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f6 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1502, -1, -1, 
			-1, -1, -1, 1872, 1691, -1, -1, 
			-1, -1, -1, 1879, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f7 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1502, -1, -1, 
			-1, -1, -1, 1872, 1691, -1, -1, 
			-1, -1, 1969, 1879, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f8 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1502, -1, -1, 
			-1, -1, -1, 1872, 1691, -1, -1, 
			-1, -1, 2060, 1879, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f9 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1502, -1, -1, 
			-1, -1, 2144, 1872, 1691, -1, -1, 
			-1, -1, 2151, 1879, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f10 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1502, -1, -1, 
			-1, -1, 2235, 1872, 1691, -1, -1, 
			-1, -1, 2151, 1879, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f11 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1502, -1, -1, 
			-1, 2325, 2326, 1872, 1691, -1, -1, 
			-1, -1, 2151, 1879, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f12 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1502, -1, -1, 
			-1, 2416, 2326, 1872, 1691, -1, -1, 
			-1, -1, 2151, 1879, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	private short[] a2f13 = {
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, 1502, -1, -1, 
			-1, 2416, 2326, 1872, 1691, -1, -1, 
			-1, 2514, 2151, 1879, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1
			};
	
	private MapAnimation animation1;
	private MapAnimation animation2;
	
	public MapAnimationManager(Core core) {
		animation1 = new MapAnimation(13, 7, new Location(Bukkit.getWorlds().get(0), 611, 32, 72), Arrays.asList(a1f0, a1f1, a1f2, a1f3, a1f4, a1f5, a1f6, a1f7, a1f8, a1f9, a1f10, a1f11, a1f12, a1f13), core);
		animation2 = new MapAnimation(13, 7, new Location(Bukkit.getWorlds().get(0), 611, 32, 72), Arrays.asList(a2f0, a2f1, a2f2, a2f3, a2f4, a2f5, a2f6, a2f7, a2f8, a2f9, a2f10, a2f11, a2f12, a2f13), core);
	}
	
	public MapAnimation getAnimationById(int id) {
		switch (id) {
			case 1:
				return animation1;
			case 2:
				return animation2;
			default:
				return null;
		}
	}
}