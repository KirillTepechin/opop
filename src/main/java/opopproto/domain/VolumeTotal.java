package opopproto.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class VolumeTotal extends Volume{
    private int total;
}
