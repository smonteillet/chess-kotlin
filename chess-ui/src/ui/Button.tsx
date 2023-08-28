import styled from 'styled-components';
import Theme from './Theme';

const Button = styled.button`
  width: 100%;
  font: inherit;
  padding: 0.5rem 1.5rem;
  border: 1px solid ${Theme.darkSquare};
  color: white;
  background: ${Theme.darkSquare};
  box-shadow: 0 0 4px rgba(0, 0, 0, 0.26);
  cursor: pointer;

  @media (min-width: 768px) {
    width: auto;
  }

  &:focus {
    outline: none;
  }

  &:hover,
  &:active {
    background: ${Theme.darkSquare};
    border-color: ${Theme.darkSquare};
    box-shadow: 0 0 8px rgba(0, 0, 0, 0.26);
  }
`;
export default Button