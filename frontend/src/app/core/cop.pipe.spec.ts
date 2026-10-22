import { CopPipe } from './cop.pipe';

describe('CopPipe', () => {
  const pipe = new CopPipe();

  it('formatea pesos colombianos sin decimales', () => {
    const resultado = pipe.transform(54900);
    expect(resultado).toContain('54.900');
    expect(resultado).not.toContain(',00');
  });

  it('devuelve cadena vacia para null o undefined', () => {
    expect(pipe.transform(null)).toBe('');
    expect(pipe.transform(undefined)).toBe('');
  });

  it('formatea el cero', () => {
    expect(pipe.transform(0)).toContain('0');
  });
});
