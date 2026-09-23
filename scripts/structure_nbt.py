#!/usr/bin/env python3
"""Small lossless NBT codec used by Vintner's structure tooling."""

from __future__ import annotations

import gzip
import struct
from pathlib import Path
from typing import TypeAlias


TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12

NbtTag: TypeAlias = tuple[int, object]
NbtCompound: TypeAlias = dict[str, NbtTag]


class Reader:
    def __init__(self, data: bytes) -> None:
        self.data = data
        self.offset = 0

    def take(self, length: int) -> bytes:
        end = self.offset + length
        if length < 0 or end > len(self.data):
            raise ValueError("truncated NBT payload")
        value = self.data[self.offset:end]
        self.offset = end
        return value

    def unpack(self, fmt: str) -> object:
        return struct.unpack(fmt, self.take(struct.calcsize(fmt)))[0]

    def unsigned_short(self) -> int:
        return int(self.unpack(">H"))

    def integer(self) -> int:
        return int(self.unpack(">i"))

    def string(self) -> str:
        return self.take(self.unsigned_short()).decode("utf-8")

    def payload(self, tag_type: int) -> object:
        if tag_type == TAG_BYTE:
            return int(self.unpack(">b"))
        if tag_type == TAG_SHORT:
            return int(self.unpack(">h"))
        if tag_type == TAG_INT:
            return self.integer()
        if tag_type == TAG_LONG:
            return int(self.unpack(">q"))
        if tag_type == TAG_FLOAT:
            return float(self.unpack(">f"))
        if tag_type == TAG_DOUBLE:
            return float(self.unpack(">d"))
        if tag_type == TAG_BYTE_ARRAY:
            return self.take(self.integer())
        if tag_type == TAG_STRING:
            return self.string()
        if tag_type == TAG_LIST:
            element_type = int(self.unpack(">b"))
            length = self.integer()
            if length < 0:
                raise ValueError("negative NBT list length")
            if element_type == TAG_END and length:
                raise ValueError(
                    "non-empty NBT list declares TAG_End at "
                    f"offset {self.offset} with length {length}"
                )
            return (
                element_type,
                [self.payload(element_type) for _ in range(length)],
            )
        if tag_type == TAG_COMPOUND:
            result: NbtCompound = {}
            while True:
                child_type = int(self.unpack(">b"))
                if child_type == TAG_END:
                    return result
                if not TAG_BYTE <= child_type <= TAG_LONG_ARRAY:
                    raise ValueError(f"invalid NBT tag type {child_type}")
                child_name = self.string()
                result[child_name] = (
                    child_type,
                    self.payload(child_type),
                )
        if tag_type == TAG_INT_ARRAY:
            length = self.integer()
            if length < 0:
                raise ValueError("negative NBT int-array length")
            return [int(self.unpack(">i")) for _ in range(length)]
        if tag_type == TAG_LONG_ARRAY:
            length = self.integer()
            if length < 0:
                raise ValueError("negative NBT long-array length")
            return [int(self.unpack(">q")) for _ in range(length)]
        raise ValueError(f"unsupported NBT tag type {tag_type}")


def decode_root(data: bytes) -> NbtCompound:
    reader = Reader(data)
    if reader.unpack(">b") != TAG_COMPOUND:
        raise ValueError("root is not an NBT compound")
    if reader.string() != "":
        raise ValueError("root compound must have an empty name")
    root = reader.payload(TAG_COMPOUND)
    if reader.offset != len(data):
        raise ValueError("trailing bytes after root compound")
    if not isinstance(root, dict):
        raise ValueError("decoded root is not a compound")
    return root


def decode_gzip(path: Path) -> NbtCompound:
    return decode_root(gzip.decompress(path.read_bytes()))


def encode_string(value: str) -> bytes:
    encoded = value.encode("utf-8")
    if len(encoded) > 0xFFFF:
        raise ValueError("NBT string exceeds unsigned-short length")
    return struct.pack(">H", len(encoded)) + encoded


def encode_payload(tag_type: int, value: object) -> bytes:
    if tag_type == TAG_BYTE:
        return struct.pack(">b", int(value))
    if tag_type == TAG_SHORT:
        return struct.pack(">h", int(value))
    if tag_type == TAG_INT:
        return struct.pack(">i", int(value))
    if tag_type == TAG_LONG:
        return struct.pack(">q", int(value))
    if tag_type == TAG_FLOAT:
        return struct.pack(">f", float(value))
    if tag_type == TAG_DOUBLE:
        return struct.pack(">d", float(value))
    if tag_type == TAG_BYTE_ARRAY:
        raw = bytes(value)
        return struct.pack(">i", len(raw)) + raw
    if tag_type == TAG_STRING:
        return encode_string(str(value))
    if tag_type == TAG_LIST:
        element_type, values = value
        return (
            struct.pack(">bi", int(element_type), len(values))
            + b"".join(
                encode_payload(int(element_type), element)
                for element in values
            )
        )
    if tag_type == TAG_COMPOUND:
        encoded = bytearray()
        for name, (child_type, child_value) in value.items():
            encoded.extend(struct.pack(">b", child_type))
            encoded.extend(encode_string(name))
            encoded.extend(encode_payload(child_type, child_value))
        encoded.extend(struct.pack(">b", TAG_END))
        return bytes(encoded)
    if tag_type == TAG_INT_ARRAY:
        values = list(value)
        return struct.pack(">i", len(values)) + b"".join(
            struct.pack(">i", int(element)) for element in values
        )
    if tag_type == TAG_LONG_ARRAY:
        values = list(value)
        return struct.pack(">i", len(values)) + b"".join(
            struct.pack(">q", int(element)) for element in values
        )
    raise ValueError(f"unsupported NBT tag type {tag_type}")


def encode_root(value: NbtCompound) -> bytes:
    return (
        struct.pack(">b", TAG_COMPOUND)
        + encode_string("")
        + encode_payload(TAG_COMPOUND, value)
    )


def encode_gzip(value: NbtCompound) -> bytes:
    return gzip.compress(encode_root(value), mtime=0)


def tag_byte(value: int) -> NbtTag:
    return TAG_BYTE, value


def tag_int(value: int) -> NbtTag:
    return TAG_INT, value


def tag_string(value: str) -> NbtTag:
    return TAG_STRING, value


def tag_list(element_type: int, values: list[object]) -> NbtTag:
    return TAG_LIST, (element_type, values)


def tag_compound(value: NbtCompound) -> NbtTag:
    return TAG_COMPOUND, value


def require_tag(
    compound: NbtCompound,
    name: str,
    expected_type: int,
) -> object:
    tag = compound.get(name)
    if tag is None or tag[0] != expected_type:
        raise ValueError(
            f"expected {name!r} to use NBT tag {expected_type}"
        )
    return tag[1]


def require_list(
    compound: NbtCompound,
    name: str,
    expected_element_type: int,
) -> list[object]:
    element_type, values = require_tag(compound, name, TAG_LIST)
    if element_type != expected_element_type:
        raise ValueError(
            f"expected {name!r} list elements to use NBT tag "
            f"{expected_element_type}"
        )
    return values
